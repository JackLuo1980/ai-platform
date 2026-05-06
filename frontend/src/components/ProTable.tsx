import { useState, useCallback } from 'react';
import { Table, Button, Space, Input, message } from 'antd';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import type { TableProps, TablePaginationConfig } from 'antd';
import type { SorterResult } from 'antd/es/table/interface';

interface ProTableProps<T extends Record<string, unknown>> extends Omit<TableProps<T>, 'dataSource' | 'pagination'> {
  fetchData: (params: Record<string, unknown>) => Promise<{ items: T[]; total: number }>;
  searchFields?: { key: string; label: string }[];
  toolbar?: React.ReactNode;
  onRowClick?: (record: T) => void;
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
    showTotal: function (total) { return `Total ${total} items`; },
  });
  const [sorter, setSorter] = useState<Record<string, string>>({});
  const [searchValues, setSearchValues] = useState<Record<string, string>>({});

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
      setData(result.items || []);
      setPagination(function (prev) { return { ...prev, total: result.total || 0 }; });
    } catch {
      message.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  }, [fetchData, pagination.current, pagination.pageSize, sorter, searchValues]);

  useState(function () {
    loadData();
  });

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
            Search
          </Button>
        </Space>
        <Space>
          {toolbar}
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            Refresh
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
          return { onClick: function () { onRowClick(record); }, style: { cursor: 'pointer' } };
        } : undefined}
        {...tableProps}
      />
    </div>
  );
}

export default ProTable;
