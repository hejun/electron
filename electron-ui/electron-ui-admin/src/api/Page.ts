export default interface Page<T> {
  pages: number

  size: number

  current: number

  total: number

  records: T[]
}
