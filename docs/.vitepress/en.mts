import {defineConfig} from 'vitepress'

export default defineConfig({
    lang: 'en-US',
    titleTemplate: false,
    themeConfig: {
        nav: [
            {
                text: 'Guide',
                link: '/en/guide',
                activeMatch: '/en/guide'
            }
        ],
        sidebar: [
            {text: 'Reference', items: []}
        ],
        footer:{
            message: 'Released under the Apache License',
            copyright: 'Copyright © HeJun'
        }
    }
})