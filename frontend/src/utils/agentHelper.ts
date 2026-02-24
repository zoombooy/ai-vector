/**
 * Agent相关的工具函数
 */

/**
 * 解析JSON字段（处理字符串或对象）
 * @param value 可能是字符串或对象的值
 * @returns 解析后的对象
 */
export const parseJsonField = (value: any): any => {
  if (!value) {
    return null
  }
  
  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch (e) {
      console.error('JSON解析失败:', e)
      return null
    }
  }
  
  if (typeof value === 'object') {
    return value
  }
  
  return null
}

/**
 * 格式化JSON字段为字符串（用于显示）
 * @param value 可能是字符串或对象的值
 * @returns 格式化后的JSON字符串
 */
export const formatJsonField = (value: any): string => {
  const parsed = parseJsonField(value)
  if (!parsed) {
    return ''
  }
  
  try {
    return JSON.stringify(parsed, null, 2)
  } catch (e) {
    console.error('JSON格式化失败:', e)
    return String(value)
  }
}

/**
 * 从Schema生成示例输入
 * @param schema JSON Schema对象或字符串
 * @returns 示例输入对象
 */
export const generateExampleFromSchema = (schema: any): any => {
  const parsed = parseJsonField(schema)
  if (!parsed || !parsed.properties) {
    return {}
  }
  
  const example: any = {}
  Object.keys(parsed.properties).forEach(key => {
    const prop = parsed.properties[key]
    if (prop.type === 'string') {
      example[key] = prop.description || prop.example || ''
    } else if (prop.type === 'number' || prop.type === 'integer') {
      example[key] = prop.example || 0
    } else if (prop.type === 'boolean') {
      example[key] = prop.example || false
    } else if (prop.type === 'array') {
      example[key] = prop.example || []
    } else if (prop.type === 'object') {
      example[key] = prop.example || {}
    }
  })
  
  return example
}

/**
 * 解析能力列表
 * @param capabilities 能力列表（可能是字符串或数组）
 * @returns 能力数组
 */
export const parseCapabilities = (capabilities: any): string[] => {
  if (!capabilities) {
    return []
  }
  
  if (Array.isArray(capabilities)) {
    return capabilities
  }
  
  if (typeof capabilities === 'string') {
    try {
      const parsed = JSON.parse(capabilities)
      if (Array.isArray(parsed)) {
        return parsed
      }
    } catch (e) {
      // 如果不是JSON，尝试按逗号分割
      return capabilities.split(',').map(s => s.trim()).filter(s => s)
    }
  }
  
  return []
}

/**
 * 解析标签列表
 * @param tags 标签（可能是字符串或数组）
 * @returns 标签数组
 */
export const parseTags = (tags: any): string[] => {
  if (!tags) {
    return []
  }
  
  if (Array.isArray(tags)) {
    return tags
  }
  
  if (typeof tags === 'string') {
    return tags.split(',').map(s => s.trim()).filter(s => s)
  }
  
  return []
}

