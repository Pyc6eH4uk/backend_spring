# Backend Spring Boot Application

## API

### GET List of sections with geologicalClasses

`GET /sections/`

### Post Create section with geologicalClasses(optional)

`POST /sections/`
    
    Content-Type: application/json
    
    {"name": "Section 1"} or {"name": "Section 1", "geologicalClasses": [{"name": "Class name", "code": "code"}]
   
### PUT Add new geologicalClasses to section or rename section

`PUT /sections/{sectionId}`

    Content-Type: application/json

    {"name": "new section name"} or {"geologicalClasses": [{"name": "Class new name", "code": "new code"}}

### DELETE Remove section with geologicalClasses by id

`DELETE /sections/{sectionId}`

### GET Get list of sections by code of geologicalClass

`GET /sections/by-code?={code}`


### POST Import xls file and store data to DB

`POST /import/`

    Content-Type: multipart/form-data
    
    Param: file
    
### GET Status of import xls file

`GET /import/{taskId}`
  

### GET Get all data from DB and make XLS file

`GET /export/`

### GET Status of exporting

`GET /export/{exportId}`

### GET Xls file(result of export data from DB)

`GET /export/{exportId}/file`

