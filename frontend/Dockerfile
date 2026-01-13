FROM nginx:alpine
RUN rm -rf /usr/share/nginx/html/*

# dashboard-ul vechi
COPY index.html      /usr/share/nginx/html/index.html
COPY script.js       /usr/share/nginx/html/script.js
COPY styles.css      /usr/share/nginx/html/styles.css

# pagina nouă de monitoring
COPY charts.html     /usr/share/nginx/html/charts.html
COPY charts.js   /usr/share/nginx/html/charts.js
COPY . /usr/share/nginx/html

EXPOSE 80
