#!/usr/bin/env sh

# abort on errors
set -e

# build
npm run docs:build
#vuepress build .

ossutil rm oss://mybaits-flex/ -rf
ossutil cp -rf assets/images  oss://mybaits-flex/assets/images
ossutil cp -rf .vitepress/dist  oss://mybaits-flex/