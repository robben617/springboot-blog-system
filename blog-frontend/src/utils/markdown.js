/**
 * 极简 Markdown 渲染器（零依赖）
 *
 * 文章正文按 Markdown 存储，详情页需要渲染成 HTML。
 * 为避免引入额外依赖，这里实现一个够用的小渲染器：
 * 标题、段落、换行、无序/有序列表、引用、分割线、
 * 围栏代码块、行内代码、加粗、斜体、删除线、链接、图片。
 *
 * 安全约定：先对原文做 HTML 转义再拼装标签，因此用户内容里的
 * <script> 等标签会被当作纯文本输出，不会被执行（防 XSS）。
 */

const PH = (i) => `\u0000PH${i}\u0000`

function escapeHtml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// 只放行安全协议，拦截 javascript: 之类的伪协议
function safeUrl(url) {
  const u = String(url).trim()
  return /^(https?:|\/|#|mailto:)/i.test(u) ? u : ''
}

function inline(text) {
  return text
    .replace(/!\[([^\]]*)\]\(([^)\s]+)\)/g, (m, alt, url) => {
      const safe = safeUrl(url)
      return safe ? `<img src="${safe}" alt="${alt}" class="md-img">` : alt
    })
    .replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, (m, label, url) => {
      const safe = safeUrl(url)
      return safe ? `<a href="${safe}" target="_blank" rel="noopener">${label}</a>` : label
    })
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/__([^_]+)__/g, '<strong>$1</strong>')
    .replace(/\*([^*\n]+)\*/g, '<em>$1</em>')
    .replace(/(^|[^\w])_([^_\n]+)_(?=[^\w]|$)/g, '$1<em>$2</em>')
    .replace(/~~([^~]+)~~/g, '<del>$1</del>')
}

export function renderMarkdown(source) {
  if (!source) return ''
  let text = String(source).replace(/\r\n?/g, '\n')

  // 1) 抽出围栏代码块，避免内部内容被后续的行内规则破坏
  const store = []
  const keep = (html) => {
    store.push(html)
    return PH(store.length - 1)
  }
  text = text.replace(/```([^\n`]*)\n([\s\S]*?)```/g, (m, lang, code) => {
    const language = lang.trim()
    const cls = language ? ` class="lang-${escapeHtml(language)}"` : ''
    return keep(`<pre class="md-code"><code${cls}>${escapeHtml(code.replace(/\n$/, ''))}</code></pre>`)
  })

  // 2) 转义剩余文本
  text = escapeHtml(text)

  // 3) 抽出行内代码，避免其中内容被加粗/斜体规则误伤
  text = text.replace(/`([^`\n]+)`/g, (m, code) => keep(`<code class="md-inline-code">${code}</code>`))

  // 4) 逐行做块级处理
  const out = []
  let para = []
  let listType = null
  let listItems = []
  let quoteLines = []

  const flushPara = () => {
    if (para.length) {
      out.push(`<p>${para.join('<br>')}</p>`)
      para = []
    }
  }
  const flushList = () => {
    if (listItems.length) {
      const tag = listType === 'ol' ? 'ol' : 'ul'
      out.push(`<${tag}>${listItems.map((item) => `<li>${item}</li>`).join('')}</${tag}>`)
      listItems = []
      listType = null
    }
  }
  const flushQuote = () => {
    if (quoteLines.length) {
      out.push(`<blockquote>${quoteLines.join('<br>')}</blockquote>`)
      quoteLines = []
    }
  }
  const flushAll = () => {
    flushPara()
    flushList()
    flushQuote()
  }

  for (const line of text.split('\n')) {
    const trimmed = line.trim()

    if (!trimmed) {
      flushAll()
      continue
    }

    // 单独成行的代码块占位符
    if (/^\u0000PH\d+\u0000$/.test(trimmed)) {
      flushAll()
      out.push(trimmed)
      continue
    }

    let m
    if ((m = /^(#{1,6})\s+(.*)$/.exec(line))) {
      flushAll()
      const level = m[1].length
      out.push(`<h${level}>${inline(m[2])}</h${level}>`)
      continue
    }
    if (/^(?:-{3,}|\*{3,}|_{3,})$/.test(trimmed)) {
      flushAll()
      out.push('<hr>')
      continue
    }
    // 转义后引用符号 > 已变为 &gt;
    if ((m = /^&gt;\s?(.*)$/.exec(line))) {
      flushPara()
      flushList()
      quoteLines.push(inline(m[1]))
      continue
    }
    if ((m = /^\s*[-*+]\s+(.*)$/.exec(line))) {
      flushPara()
      flushQuote()
      if (listType === 'ol') flushList()
      listType = 'ul'
      listItems.push(inline(m[1]))
      continue
    }
    if ((m = /^\s*\d+[.)]\s+(.*)$/.exec(line))) {
      flushPara()
      flushQuote()
      if (listType === 'ul') flushList()
      listType = 'ol'
      listItems.push(inline(m[1]))
      continue
    }

    flushList()
    flushQuote()
    para.push(inline(line))
  }
  flushAll()

  // 5) 还原占位符
  return out.join('\n').replace(/\u0000PH(\d+)\u0000/g, (m, i) => store[Number(i)] || '')
}

/** 粗略估算纯文本字数（用于编辑器统计，去掉 Markdown 标记） */
export function countWords(source) {
  if (!source) return 0
  return String(source)
    .replace(/```[\s\S]*?```/g, '')
    .replace(/[#*`_~>\-]/g, '')
    .replace(/\s/g, '')
    .length
}

export default renderMarkdown
