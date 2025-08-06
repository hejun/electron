# Electron [![License][licenseBadge]][licenseLink]

Micro Services Platform

[licenseBadge]: https://img.shields.io/badge/LICENSE-Apache%202.0-4EB1BA.svg
[licenseLink]: https://www.apache.org/licenses/LICENSE-2.0.html

## 生产服 `Nginx` 部分配置

```nginx
location /api/ {
  proxy_pass       http://127.0.0.1:8000/;

  ...

  # 用于代理302授权服务器跳转
  proxy_redirect  / /api/;
}
```