From openjdk:13-alpine
LABEL maintainer="Flo S"

RUN apk --update add imagemagick
RUN apk add --no-cache file
RUN apk add --no-cache bash
RUN wget -q https://github.com/ViSIT-Dev/compressioncontainer/raw/master/java/Compression/dist/Compression.jar -P /root/
RUN mkdir -p /root/compression

EXPOSE 1613

CMD ["java", "-jar", "/root/Compression.jar"]
