#!/usr/bin/env bash
set -euo pipefail

MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:9000}"
MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-aiplatform}"
MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-AiPlatform2026Minio!}"
BUCKET="marketplace"
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGUSER="${PGUSER:-aiplatform}"
PGDATABASE="${PGDATABASE:-aiplatform}"
PGPASSWORD="${PGPASSWORD:-AiPlatform2026Pg!}"
export PGPASSWORD

export AWS_ACCESS_KEY_ID="$MINIO_ACCESS_KEY"
export AWS_SECRET_ACCESS_KEY="$MINIO_SECRET_KEY"
export AWS_ENDPOINT_URL="$MINIO_ENDPOINT"

WORKDIR=$(mktemp -d)
trap "rm -rf $WORKDIR" EXIT

echo "==> Ensuring bucket exists"
which mc >/dev/null 2>&1 || { echo "mc (MinIO Client) required"; exit 1; }
mc alias set minio "$MINIO_ENDPOINT" "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY" 2>/dev/null
mc mb minio/$BUCKET --ignore-existing

MODELS=(
    "resnet50 torchvision ResNet-50 ImageNet-1k 1k-class image-classification 25.6MB"
    "efficientnet-b0 torchvision EfficientNet-B0 ImageNet-1k 1k-class image-classification 5.3MB"
    "mobilenetv3-small torchvision MobileNetV3-Small ImageNet-1k 1k-class image-classification 2.9MB"
    "yolov8n ultralytics YOLOv8n COCO-80 80-class object-detection 6.2MB"
    "bert-base-chinese huggingface/bert-base-chinese BERT-Base-Chinese Chinese-Wiki 21128-vocab feature-extraction 390MB"
)

for entry in "${MODELS[@]}"; do
    read -r model_id source name dataset classes task size <<<"$entry"
    echo "==> Processing $name ($model_id)"

    DEST="$WORKDIR/$model_id"
    mkdir -p "$DEST"

    case "$source" in
        torchvision)
            python3 -c "
import torch, torchvision, json, sys
weights = '${name}' + '_Weights.DEFAULT'
try:
    w = getattr(torchvision.models, '${model_id}'.replace('-','_') if '${model_id}' != 'resnet50' else 'ResNet50_Weights').DEFAULT
except AttributeError:
    from torchvision.models import resnet50, ResNet50_Weights; w = ResNet50_Weights.DEFAULT
m = torchvision.models.${model_id//-/_}(weights=w)
torch.save(m.state_dict(), '$DEST/model.pt')
meta = {'model_id': '${model_id}', 'name': '${name}', 'source': '${source}', 'task': '${task}', 'dataset': '${dataset}', 'size': '${size}'}
json.dump(meta, open('$DEST/metadata.json','w'), indent=2)
print('OK')
" 2>&1 | tail -1
            ;;
        ultralytics)
            python3 -c "
from ultralytics import YOLO
m = YOLO('${model_id}.pt')
m.save('$DEST/model.pt')
import json
meta = {'model_id': '${model_id}', 'name': '${name}', 'source': '${source}', 'task': '${task}', 'dataset': '${dataset}', 'size': '${size}'}
json.dump(meta, open('$DEST/metadata.json','w'), indent=2)
print('OK')
" 2>&1 | tail -1
            ;;
        huggingface/*)
            HF_NAME="${source#huggingface/}"
            python3 -c "
from transformers import AutoModel, AutoTokenizer
import json
AutoModel.from_pretrained('${HF_NAME}').save_pretrained('$DEST')
AutoTokenizer.from_pretrained('${HF_NAME}').save_pretrained('$DEST')
meta = {'model_id': '${model_id}', 'name': '${name}', 'source': 'huggingface', 'task': '${task}', 'dataset': '${dataset}', 'size': '${size}'}
json.dump(meta, open('$DEST/metadata.json','w'), indent=2)
print('OK')
" 2>&1 | tail -1
            ;;
    esac

    echo "  Uploading to MinIO..."
    aws s3 sync "$DEST" "s3://$BUCKET/$model_id/" --endpoint-url "$MINIO_ENDPOINT" 2>/dev/null \
        || mc cp --recursive "$DEST/" "minio/$BUCKET/$model_id/"

    echo "  Registering in DB..."
    psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -c \
        "INSERT INTO marketplace_models (model_id, name, source, task, dataset, size, storage_path, created_at)
         VALUES ('$model_id', '$name', '$source', '$task', '$dataset', '$size', 's3://$BUCKET/$model_id/', NOW())
         ON CONFLICT (model_id) DO UPDATE SET name=EXCLUDED.name, updated_at=NOW();"
done

echo "==> Marketplace seeded with ${#MODELS[@]} models"
