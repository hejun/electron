import {defineConfig} from 'vitepress'

export default defineConfig({
    lang: 'zh-Hans',
    titleTemplate: false,
    themeConfig: {
        nav: [
            {
                text: '指南',
                link: '/guide',
                activeMatch: '/guide'
            }
        ],
        sidebar: [
            {text: '参考', items: []}
        ],
        footer:{
            message: '基于 Apache 许可发布',
            copyright: '版权所有 © HeJun'
        }
    }
})