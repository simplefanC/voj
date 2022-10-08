import {sidebar} from "vuepress-theme-hope";

export default sidebar([
  // "/",
  // "/home",
  // "/slide",
  {
    text: '序章',
    collapsable: true,
    prefix: "/introduction/",
    children: [
      '',
      'architecture'
    ]
  },
  {
    text: '快速部署',
    collapsable: true,
    prefix: "/deploy/",
    children: [
      '',
      'docker',
      'open-https',
      'multi-judgeserver',
      // 'update',
      'how-to-backup'
    ]
  },
  {
    text: '单体部署',
    collapsable: true,
    prefix: "/monomer/",
    children: [
      'mysql',
      // 'mysql-checker',
      'redis',
      'nacos',
      'backend',
      'judgeserver',
      'frontend',
      'rsync'
    ]
  },
  {
    text: '开发文档',
    collapsable: true,
    prefix: "/develop/",
    children: [
      'db',
      'judge_dispatcher',
      'sandbox',
      'update-fe'
    ]
  },
  {
    text: '使用文档',
    collapsable: true,
    prefix: "/use/",
    children: [
      'import-problem',
      'judge-mode',
      'testcase',
      'training',
      'contest',
      // 'group',
      'import-user',
      'admin-user',
      'notice-announcement',
      'discussion-admin'
      // 'custom-difficulty',
      // 'close-free-cdn'
    ]
  },
  // {
  //   text: "如何使用",
  //   icon: "creative",
  //   prefix: "/guide/",
  //   link: "/guide/",
  //   children: "structure",
  // },
  // {
  //   text: "文章",
  //   icon: "note",
  //   prefix: "/posts/",
  //   children: [
  //     {
  //       text: "文章 1-4",
  //       icon: "note",
  //       collapsable: true,
  //       prefix: "article/",
  //       children: ["article1", "article2", "article3", "article4"],
  //     },
  //     {
  //       text: "文章 5-12",
  //       icon: "note",
  //       children: [
  //         {
  //           text: "文章 5-8",
  //           icon: "note",
  //           collapsable: true,
  //           prefix: "article/",
  //           children: ["article5", "article6", "article7", "article8"],
  //         },
  //         {
  //           text: "文章 9-12",
  //           icon: "note",
  //           children: ["article9", "article10", "article11", "article12"],
  //         },
  //       ],
  //     },
  //   ],
  // },
]);
