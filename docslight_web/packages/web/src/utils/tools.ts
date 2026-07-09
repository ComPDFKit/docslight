/** 操作系统使用的文件大小计算基数 */
export const getSystemBaseUnit = () => {
  // 优先尝试API检测
  try {
    const formatter = new Intl.NumberFormat(undefined, {
      style: 'unit',
      unit: 'gigabyte'
    })
    return formatter.resolvedOptions().unitDisplay === 'short' ? 1000 : 1024
  } catch {
    // 次选用户代理分析
    const ua = navigator.userAgent
    // Windows/Mac通常使用1024，Linux发行版可能使用1000
    return /(Windows|Macintosh|Mac OS)/i.test(ua) ? 1024 : 1000
  }
}
