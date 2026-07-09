declare module 'markdown-it' {
  interface MarkdownIt {
    render(md: string, env?: any): string
    use(plugin: any, ...options: any[]): MarkdownIt
  }

  interface MarkdownItConstructor {
    new (presetName?: string, options?: any): MarkdownIt
    (presetName?: string, options?: any): MarkdownIt
  }

  const MarkdownIt: MarkdownItConstructor
  export default MarkdownIt
}

declare module 'markdown-it-katex' {
  import type MarkdownIt from 'markdown-it'
  const mk: (md: MarkdownIt, options?: any) => void
  export default mk
}
