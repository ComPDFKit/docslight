// 定义运行时环境对象类型
interface RuntimeEnv {
  [key: string]: string;
}

// 声明全局环境变量
declare global {
  interface Window {
    __app_env?: RuntimeEnv;
  }
}

export const getEnv = (key: string): string => {
  // 优先使用运行时配置
  if (window.__app_env && window.__app_env[key] !== undefined) {
    return window.__app_env[key];
  }

  key = key.indexOf('VITE_') === 0 ? key : `VITE_${key}`;
  // 回退到构建时环境变量
  return import.meta.env[key]
}
