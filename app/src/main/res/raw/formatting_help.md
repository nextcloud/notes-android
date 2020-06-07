# Context based formatting

A major design goal of the Notes app is to provide a distraction free tool. Though you will be able to format your texts with Markdown. For various of the below mentioned examples, you can use shortcuts so you can format your notes without typing in the codes below.
Just select a range of text or tap on your cursor at any position and you will get a popup menu which contains next to the default entries `Cut`, `Copy`, `Select all` entries like `Link` or `Checkbox`.

---

# Text

It's very easy to make some words **bold** and other words *italic* with Markdown. You can ~~strike~~ some words through and even [link to Google](http://google.com).

```
It's very easy to make some words **bold** and other words *italic* with Markdown. You can ~~strike~~ some words through and even [link to Google](http://google.com).
```

---

# Lists

Sometimes you want numbered lists:

1. One
2. Two
3. Three

Sometimes you want bullet points:

* Start a line with a star
* Profit!

Alternatively,

- Dashes work just as well
- And if you have sub points, put two spaces before the dash or star:
  - Like this
  - And this

```
Sometimes you want numbered lists:

1. One
2. Two
3. Three

Sometimes you want bullet points:

* Start a line with a star
* Profit!

Alternatively,

- Dashes work just as well
- And if you have sub points, put two spaces before the dash or star:
  - Like this
  - And this
```

---

# Checkbox

To create a checkbox, use a list followed by brackets

- [ ] Item 1
* [ ] Item 2

```
To create a checkbox, use a list followed by brackets

- [ ] Item 1
* [ ] Item 2
```

---

# Structured documents

Sometimes it's useful to have different levels of headings to structure your documents. Start lines with a `#` to create headings. Multiple `##` in a row denote smaller heading sizes.

### This is a third-tier heading

You can use one `#` all the way up to `######` six for different heading sizes.

If you'd like to quote someone, use the > character before the line:

> Coffee. The finest organic suspension ever devised... I beat the Borg with it.
> - Captain Janeway

```
# Structured documents

Sometimes it's useful to have different levels of headings to structure your documents. Start lines with a `#` to create headings. Multiple `##` in a row denote smaller heading sizes.

### This is a third-tier heading

You can use one `#` all the way up to `######` six for different heading sizes.

If you'd like to quote someone, use the > character before the line:

> Coffee. The finest organic suspension ever devised... I beat the Borg with it.
> - Captain Janeway
```

---

# Code

There are many different ways to style code with Markdown. If you have inline code blocks, wrap them in backticks:

\`var example = true\`
`var example = true`

Markdown also supports something called code fencing, which allows for multiple lines without indentation:

\`\`\`
if (isAwesome){
  return true
}
\`\`\`

```
if (isAwesome){
  return true
}
```

And if you'd like to use syntax highlighting, include the language:

\`\`\`javascript
if (isAwesome){
  return true
}
\`\`\`

```javascript
if (isAwesome){
  return true
}
```

---

# Unsupported

While we try to continuously improve the support for Markdown, there are a few features which are not yet supported by Notes:

- Tables
- Images

If you are interested in contributing support for one of those features, get in contact with us via GitHub or E-Mail.