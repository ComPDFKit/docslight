// utils/useJsonExport.ts
import { saveAs } from "file-saver";
import * as XLSX from "xlsx";
import Papa from "papaparse";

export interface JsonDetailItem {
  page_id: number;
  paragraph_id: number;
  type: string;
  text?: string;
  image_url?: string;
}

export interface JsonData {
  detail: JsonDetailItem[];
}

/**
 * JSON -> Markdown
 */
export function json2markdown(jsonData: JsonData, filename = "result.md") {
  const pages: Record<number, JsonDetailItem[]> = {};
  jsonData.detail.forEach(item => {
    pages[item.page_id] = pages[item.page_id] || [];
    pages[item.page_id].push(item);
  });

  const markdown: string[] = [];
  Object.keys(pages).sort((a, b) => +a - +b).forEach(pageId => {
    markdown.push(`## Page ${pageId}`);
    pages[+pageId]
      .sort((a, b) => a.paragraph_id - b.paragraph_id)
      .forEach(para => {
        const text = para.text || "";
        switch (para.type) {
          case "title": markdown.push(`# ${text}\n`); break;
          case "paragraph":
          case "reference":
          case "catalogue": markdown.push(text); break;
          case "image":
          case "formula": markdown.push(`![Image](${para.image_url || ""})\n`); break;
          case "standard_table":
          case "unstandard_table":
          case "table": markdown.push(text + "\n"); break;
          case "ordered_list":
          case "unordered_list": markdown.push(text); break;
          case "code":
          case "algorithm": markdown.push(`\`\`\`\n${text}\n\`\`\``); break;
          case "header":
          case "footer":
          case "page_number": markdown.push(`<!-- ${text} -->`); break;
          case "table_title":
          case "figure_title": markdown.push(`**${text}**`); break;
          case "table_caption":
          case "figure_caption": markdown.push(`_${text}_`); break;
        }
      });
  });

  const blob = new Blob([markdown.join("\n")], { type: "text/markdown;charset=utf-8" });
  saveAs(blob, filename);
}

/**
 * JSON -> TXT
 */
export function json2txt(jsonData: JsonData, filename = "result.txt") {
  const pages: Record<number, JsonDetailItem[]> = {};
  jsonData.detail.forEach(item => {
    pages[item.page_id] = pages[item.page_id] || [];
    pages[item.page_id].push(item);
  });

  const lines: string[] = [];
  Object.keys(pages).sort((a, b) => +a - +b).forEach(pageId => {
    lines.push(`Page ${pageId}`);
    pages[+pageId]
      .sort((a, b) => a.paragraph_id - b.paragraph_id)
      .forEach(para => {
        const text = para.text || "";
        switch (para.type) {
          case "title": lines.push(`${text}\n`); break;
          case "paragraph":
          case "reference":
          case "catalogue": lines.push(text); break;
          case "image":
          case "formula": lines.push(`[Image: ${para.image_url || ""}]`); break;
          case "standard_table":
          case "unstandard_table":
          case "table":
            lines.push("Table:");
            const doc = new DOMParser().parseFromString(text, "text/html");
            doc.querySelectorAll("tr").forEach(row => {
              const rowData = Array.from(row.querySelectorAll("td, th")).map(cell => cell.textContent?.trim() || "");
              lines.push(rowData.join("\t"));
            });
            break;
          case "ordered_list":
          case "unordered_list": lines.push(text); break;
          case "code":
          case "algorithm": lines.push(`\`\`\`\n${text}\n\`\`\``); break;
          case "header":
          case "footer":
          case "page_number": lines.push(`[${text}]`); break;
          case "table_title":
          case "figure_title": lines.push(`**${text}**`); break;
          case "table_caption":
          case "figure_caption": lines.push(`_${text}_`); break;
        }
      });
  });

  const blob = new Blob([lines.join("\n")], { type: "text/plain;charset=utf-8" });
  saveAs(blob, filename);
}

/**
 * JSON -> Excel (提取 type=table 的 HTML)
 */
export function json2excel(jsonData: JsonData, filename = "result.xlsx") {
  const tables: { pageId: number; html: string }[] = [];
  jsonData.detail.forEach(item => {
    if (["standard_table", "unstandard_table", "table"].includes(item.type) && item.text) {
      tables.push({ pageId: item.page_id, html: item.text });
    }
  });

  const wb = XLSX.utils.book_new();
  tables.forEach((tbl, idx) => {
    const doc = new DOMParser().parseFromString(tbl.html, "text/html");
    const rows = Array.from(doc.querySelectorAll("tr")).map(tr =>
      Array.from(tr.querySelectorAll("td, th")).map(td => td.textContent?.trim() || "")
    );
    const ws = XLSX.utils.aoa_to_sheet(rows);
    XLSX.utils.book_append_sheet(wb, ws, `Page${tbl.pageId}_Table${idx + 1}`);
  });

  const wbout = XLSX.write(wb, { bookType: "xlsx", type: "array" });
  saveAs(new Blob([wbout], { type: "application/octet-stream" }), filename);
}

/**
 * JSON -> CSV (提取 type=table 的 HTML)
 */
export function json2csv(jsonData: JsonData, filename = "result.csv") {
  const tables: string[][] = [];
  jsonData.detail.forEach(item => {
    if (["standard_table", "unstandard_table", "table"].includes(item.type) && item.text) {
      const doc = new DOMParser().parseFromString(item.text, "text/html");
      const rows = Array.from(doc.querySelectorAll("tr")).map(tr =>
        Array.from(tr.querySelectorAll("td, th")).map(td => td.textContent?.trim() || "")
      );
      tables.push(...rows);
    }
  });

  const csv = Papa.unparse(tables);
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8" });
  saveAs(blob, filename);
}

/**
 * JSON -> Table TXT
 */
export function json2tabletxt(jsonData: JsonData, filename = "table.txt") {
  const lines: string[] = [];
  jsonData.detail.forEach(item => {
    if (["standard_table", "unstandard_table", "table"].includes(item.type) && item.text) {
      const doc = new DOMParser().parseFromString(item.text, "text/html");
      const rows = Array.from(doc.querySelectorAll("tr")).map(tr =>
        Array.from(tr.querySelectorAll("td, th")).map(td => td.textContent?.trim() || "")
      );
      rows.forEach(row => lines.push(row.join("\t")));
      lines.push("");
    }
  });

  const blob = new Blob([lines.join("\n")], { type: "text/plain;charset=utf-8" });
  saveAs(blob, filename);
}

/**
 * JSON 转 Markdown (只保留 table 类型内容)
 */
export function json2tableMd(jsonData: JsonData, filename = "table.md") {
  const lines: string[] = [];

  // 按 page_id 分组
  const pages: Record<number, typeof jsonData.detail> = {};
  jsonData.detail.forEach(item => {
    if (!pages[item.page_id]) {
      pages[item.page_id] = [];
    }
    pages[item.page_id].push(item);
  });

  // 按 page_id 排序
  const sortedPages = Object.keys(pages).map(Number).sort((a, b) => a - b);

  for (const pageId of sortedPages) {
    lines.push(`## Page ${pageId}`);

    // 段落按 paragraph_id 排序
    const paragraphs = pages[pageId].sort((a, b) => a.paragraph_id - b.paragraph_id);

    for (const para of paragraphs) {
      const type = para.type;
      const text = para.text || "";

      switch (type) {
        case "standard_table":
        case "unstandard_table":
        case "table":
          lines.push(text);
          lines.push(""); // 空行分隔
          break;
        // case "table_title":
        //   lines.push(`**${text}**`);
        //   break;
      }
    }
  }

  const blob = new Blob([lines.join("\n")], { type: "text/markdown;charset=utf-8" });
  saveAs(blob, filename);
}

export type ExportType = "md" | "txt" | "excel" | "csv" | "tabletxt" | "tablemd";

export function json2file(jsonData: JsonData, type: ExportType, filename?: string) {
  switch (type) {
    case "md":
      json2markdown(jsonData, filename);
      break;
    case "txt":
      json2txt(jsonData, filename);
      break;
    case "excel":
      json2excel(jsonData, filename);
      break;
    case "csv":
      json2csv(jsonData, filename);
      break;
    case "tabletxt":
      json2tabletxt(jsonData, filename);
      break;
    case "tablemd":
      json2tableMd(jsonData, filename);
      break;
    default:
      throw new Error(`Unsupported export type: ${type}`);
  }
}
