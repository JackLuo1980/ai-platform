import { useState, useEffect, useCallback, useRef } from 'react';
import { Table, Button, Space, Input, message } from 'antd';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import type { TableProps, TablePaginationConfig } from 'antd';
import type { SorterResult } from 'antd/es/table/interface';

interface ProTableProps<T extends Record<string, unknown>> extends Omit<TableProps<T>, 'dataSource' | 'pagination'> {
  fetchData: (params: Record<string, unknown>) => Promise<unknown>;
  searchFields?: { key: string; label: string }[];
  toolbar?: React.ReactNode;
  onRowClick?: (record: T) => void;
}

function extractPageData(res: unknown): { items: unknown[]; total: number } {
  const r = res as Record<string, unknown>;
  if (r?.data && typeof r.data === 'object') {
    const d = r.data as Record<string, unknown>;
    if (Array.isArray(d.items)) {
      return { items: d.items, total: (d.total as number) || d.items.length };
    }
    if (Array.isArray(d.content)) {
      return { items: d.content, total: (d.total as number) || (d.totalElements as number) || d.content.length };
    }
    if (Array.isArray(d.list)) {
      return { items: d.list, total: (d.total as number) || d.list.length };
    }
  }
  if (Array.isArray(r?.items)) {
    return { items: r.items as unknown[], total: (r.total as number) || (r.items as unknown[]).length };
  }
  if (Array.isArray(r?.content)) {
    return { items: r.content as unknown[], total: (r.total as number) || (r.totalElements as number) || (r.content as unknown[]).length };
  }
  if (Array.isArray(res)) {
    return { items: res as unknown[], total: res.length };
  }
  return { items: [], total: 0 };
}

function ProTable<T extends Record<string, unknown>>(props: ProTableProps<T>) {
  const { fetchData, searchFields, toolbar, onRowClick, ...tableProps } = props;
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showTotal: function (total) { return `\u603B\u8BA1 ${total} \u6761`; },
  });
  const [sorter, setSorter] = useState<Record<string, string>>({});
  const [searchValues, setSearchValues] = useState<Record<string, string>>({});
  const mountedRef = useRef(true);

  const loadData = useCallback(async function () {
    setLoading(true);
    try {
      const params: Record<string, unknown> = {
        page: (pagination.current || 1) - 1,
        size: pagination.pageSize || 20,
        ...sorter,
        ...searchValues,
      };
      const result = await fetchData(params);
      if (!mountedRef.current) return;
      const extracted = extractPageData(result);
      setData(extracted.items as T[]);
      setPagination(function (prev) { return { ...prev, total: extracted.total }; });
    } catch {
      message.error('\u52A0\u8F7D\u5931\u8D25');
    } finally {
      if (mountedRef.current) setLoading(false);
    }
  }, [fetchData, pagination.current, pagination.pageSize, sorter, searchValues]);

  useEffect(function () {
    mountedRef.current = true;
    loadData();
    return function () { mountedRef.current = false; };
  }, [loadData]);

  function handleTableChange(
    pag: TablePaginationConfig,
    _filters: Record<string, unknown>,
    sort: SorterResult<T> | SorterResult<T>[]
  ) {
    const s = Array.isArray(sort) ? sort[0] : sort;
    setPagination(pag);
    if (s.field) {
      setSorter({ sort: s.field as string, order: s.order === 'ascend' ? 'asc' : 'desc' });
    } else {
      setSorter({});
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16, flexWrap: 'wrap', gap: 8 }}>
        <Space wrap>
          {searchFields
            ? searchFields.map(function (f) {
                return (
                  <Input
                    key={f.key}
                    placeholder={f.label}
                    value={searchValues[f.key]}
                    onChange={function (e) {
                      setSearchValues(function (prev) { return { ...prev, [f.key]: e.target.value }; });
                    }}
                    onPressEnter={loadData}
                    prefix={<SearchOutlined />}
                    style={{ width: 200 }}
                  />
                );
              })
            : null}
          <Button type="primary" icon={<SearchOutlined />} onClick={loadData}>
            \u641C\u7D22
          </Button>
        </Space>
        <Space>
          {toolbar}
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            \u5237\u65B0
          </Button>
        </Space>
      </div>
      <Table<T>
        rowKey="id"
        loading={loading}
        dataSource={data}
        pagination={pagination}
        onChange={handleTableChange as any}
        onRow={onRowClick ? function (record) {
          return { onClick: function () { onRowClick(record); }, style: { cursor: 'pointer' } } as any;
        } : undefined}
        {...tableProps}
      />
    </div>
  );
}

export default ProTable;
