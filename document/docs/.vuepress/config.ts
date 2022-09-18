import {defineUserConfig} from "vuepress";
import theme from "./theme";

// const { searchPlugin } = require('@vuepress/plugin-search')

export default defineUserConfig({
  // dest: './',   // 设置输出目录
  lang: "zh-CN",
  title: "VOJ",
  description: "Virtual Online Judge",

  base: "/",

  head: [
    [
      "link",
      {
        rel: "stylesheet",
        href: "//at.alicdn.com/t/font_2410206_mfj6e1vbwo.css",
      },
    ],
  ],

  plugins: [
    // searchPlugin({
    //   // https://v2.vuepress.vuejs.org/zh/reference/plugin/search.html
    //   // 排除首页
    //   isSearchable: (page) => page.path !== "/",
    //   maxSuggestions: 10,
    //   hotKeys: ["s", "/"],
    //   // 用于在页面的搜索索引中添加额外字段
    //   getExtraFields: () => [],
    //   locales: {
    //     "/": {
    //       placeholder: "搜索",
    //     },
    //   },
    // }),
  ],
  theme,
});
