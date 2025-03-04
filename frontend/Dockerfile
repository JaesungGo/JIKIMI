FROM node:16 as build

WORKDIR /app

COPY package*.json ./

ENV NODE_OPTIONS="--max-old-space-size=2048"

RUN npm install

COPY . .

RUN npm run build

FROM nginx:stable-alpine as production-stage

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]