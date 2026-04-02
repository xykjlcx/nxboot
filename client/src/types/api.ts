/** 后端统一返回结构 */
export interface R<T> {
  code: number;
  msg: string;
  data: T;
}

/** 分页返回结构 */
export interface PageResult<T> {
  list: T[];
  total: number;
}

/** 分页查询参数 */
export interface PageQuery {
  pageNum?: number;
  pageSize?: number;
}
