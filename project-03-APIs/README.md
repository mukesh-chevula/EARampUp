# User Management System
This System allows to Create, Read, Update & Delete (CRUD) Operation over User Data.

## Tech Stack 
* SpringBoot
* SpringBoot Web
* Gradle
* HTTP
* Postman

## Methods

### Create a User 

`POST /users`

#### BODY:

```json
{
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "EA",
    "experienceMonths": 12,
    "mobileNumber": "9876543210"
}
```

#### Response:

```json
{
    "userId": 4,
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "EA",
    "experienceMonths": 12,
    "mobileNumber": "9876543210"
}
```

### Get User by userId

`GET /users/{id}`<br>

`GET /users/{4}`
```json
{
    "userId": 4,
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "EA",
    "experienceMonths": 12,
    "mobileNumber": "9876543210"
}
```

### Get All Users

`GET /users`

```json
[
    {
        "userId": 2,
        "firstName": "Daivik",
        "lastName": null,
        "companyName": "Qualcomm",
        "experienceMonths": 12,
        "mobileNumber": "8185929110"
    },
    {
        "userId": 3,
        "firstName": "Daivik",
        "lastName": "B",
        "companyName": "Qualcomm",
        "experienceMonths": 12,
        "mobileNumber": "8185929110"
    },
    {
        "userId": 4,
        "firstName": "Mukesh",
        "lastName": "Ch",
        "companyName": "EA",
        "experienceMonths": 12,
        "mobileNumber": "9876543210"
    }
]
```
### Update a user 

`PUT /users/{id}`

`PUT /users/4`


#### BODY:

```json
{
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "Electronic Arts",
    "experienceMonths": 12,
    "mobileNumber": "9876543210"
}
```

#### Response:

```json
{
    "userId": 4,
    "firstName": "Mukesh",
    "lastName": "Ch",
    "companyName": "Electronic Arts",
    "experienceMonths": 12,
    "mobileNumber": "9876543210"
}
```

### Delete a user

`DELETE /users/{id}`

`DELETE /users/4`

#### Response 

```json
true
```