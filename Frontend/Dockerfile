# Etapa 1: Compilar Angular en modo producción
FROM node:20 as build
WORKDIR /app

# Copiar package.json y package-lock.json primero para aprovechar cache
COPY ProyectoVenta/package*.json ./
RUN npm install

# Copiar el resto del código Angular
COPY ProyectoVenta/ ./

# Compilar en modo producción
RUN npm run build -- --configuration production

# Etapa 2: Servir con Nginx
FROM nginx:alpine

# Copiar el build generado en la etapa anterior
COPY --from=build /app/dist/ProyectoVenta/browser/ /usr/share/nginx/html

# Copiar configuración de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

