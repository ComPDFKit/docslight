// 定义文档解析器类型枚举
export enum DocumentParserType {
  Naive = 'naive',
  Manual = 'manual',
  Paper = 'paper',
  Book = 'book',
  Laws = 'laws',
  Presentation = 'presentation',
  One = 'one',
  Qa = 'qa',
  Table = 'table',
  Picture = 'picture'
}

// 定义文件扩展名类型
export type FileExtension = string;

// 定义解析器映射类型
export type ParserMap = Map<FileExtension[], DocumentParserType[]>;
