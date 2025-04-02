import {defineConfig} from 'vitepress'
import zh from './zh.mts'
import en from './en.mts'

export default defineConfig({
    base: '/electron/',
    rewrites: {
        'zh/:rest*': ':rest*'
    },
    title: 'Electron',
    description: 'Electron Cloud',
    cleanUrls: true,
    metaChunk: true,
    locales: {
        root: {label: '简体中文', ...zh},
        en: {label: 'English', ...en}
    },
    themeConfig: {
        socialLinks: [
            {icon: 'github', link: 'https://github.com/hejun/electron'}
        ],
    }
})