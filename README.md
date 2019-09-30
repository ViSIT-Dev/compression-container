# compressioncontainer

## Setup

To set up the compression component perform the following steps in a docker container running on an LAS. This docker container has to share a common directory with the media database container and is. Furthermore a JRE is required on the compression container.

1. Copy the file "java/Compression/dist/Compression.jar" to any directory with write permissions
2. Go to this directory and run the jar file via "java -jar Compression.jar"
3. Log file, configuration file and an internal database file will be created within that directory automatically
4. Adapt the configuration file "config.ini" to your needs. Colons, equal signs and sharp symbols have to be escaped. The most important settings probably will be 
    * the "mediaFileRootDirectory" property specifying the location of the media files in the docker container's file system, 
    * the "apiPort" property defining the port number to be used for accessing the api and the web interface and
    * the "metadbApiMediaUidPrefix" specifying the prefix in the UID of all the media entities stored in the underlying meta database
    * the "metadbApiAuthString" value sent along with meta database requests for authorization
5. Restart the jar file.
6. The web interface then can be accessed via http://DOMAIN:PORT/index.html, the api is available at http://DOMAIN:PORT/api.

## Dispatch compression jobs via API

To dispatch a new compression job to the compression system, send a HTTP-POST-request to /api/jobs/dispatch with JSON-encoded POST-data containing the following values

   * "basePath": The location of the source file with respect to the specified media file root directory
   * "objectUid": The UID of the meta entity the media file is representing *without* the prefix used in the meta database
   * "mediaUid": The UID of the media source file *without* the prefix used in the meta database
   * "title": An arbitrary title for the compression job, which is just used for archive and surveillance purposes
   * "mimeType": The MIME-type of the source file, which has to be one of the following:
      * "text/plain" for 3D-OBJ-files
      * "text/jpeg" for JPEG-images
      * "text/png" for PNG-images
   * "levels": For 3D-OBJ-files: An array containing the desired vertex counts and/or "Automatisch" (in array), if models of all resolutions specified in the configuration shall be created.
   
   ## Further API methods
   
are implemented and will be specified in future.

## Docker Run
* docker run -d --name compression -p 1613:1613 --volumes-from visit --restart unless-stopped visitapp/compressioncontainer


## Docker Build
* docker build  -t "visitapp/compressioncontainer" .
* docker rmi visitapp/compressioncontainer
* docker image prune
