FROM nginx:1.25-alpine
RUN addgroup -S nginxuser && adduser -S nginxuser -G nginxuser
COPY *.html /usr/share/nginx/html/
COPY css/ /usr/share/nginx/html/css/
COPY js/ /usr/share/nginx/html/js/
COPY images/ /usr/share/nginx/html/images/
COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN chown -R nginxuser:nginxuser /usr/share/nginx/html \
    && chown -R nginxuser:nginxuser /var/cache/nginx \
    && touch /var/run/nginx.pid \
    && chown nginxuser:nginxuser /var/run/nginx.pid
USER nginxuser
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
