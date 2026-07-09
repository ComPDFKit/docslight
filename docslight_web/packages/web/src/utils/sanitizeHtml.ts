import DOMPurify from 'dompurify'

const SAFE_URI_PATTERN = /^(?:(?:https?|mailto|tel|blob):|data:image\/(?:png|gif|jpe?g|webp|svg\+xml);base64,|\/|\.\/|\.\.\/|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i

const BASE_CONFIG = {
  ALLOWED_URI_REGEXP: SAFE_URI_PATTERN,
  FORBID_TAGS: ['script', 'iframe', 'object', 'embed', 'link', 'meta', 'base'],
  FORBID_ATTR: ['srcdoc']
}

export const sanitizeHtml = (html: string = '') => {
  return DOMPurify.sanitize(html, BASE_CONFIG)
}

export const sanitizeTableHtml = (html: string = '') => {
  return DOMPurify.sanitize(html, {
    ...BASE_CONFIG,
    FORBID_TAGS: [...BASE_CONFIG.FORBID_TAGS, 'form', 'input', 'button', 'textarea', 'select', 'option']
  })
}
