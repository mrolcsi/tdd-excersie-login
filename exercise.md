Goal
----
Create an application that allows signing in and extending an expired authentication before landing on the home view,

User Stories
----

As a User, I want that the application requires me to log in, when I start the application without having an access token, so that I can onboard the application.

**Acceptance criteria:**
* When the app is started and there is no authenticated user, it presents a Login view.	
* The Login view has
    * Username field	
    * Password field with the possibility to show/hide the password
    * Login button	
* The Login button is disabled while either input fields are empty.	
* The app shows a progress indication while the login is in progress.	
* When login fails with  HTTP 401, the app shows an error alert saying “Invalid username or password.”.	
* When the login fails due to a broken internet connection, the app shows an error alert saying “Connection broken. Verify that you are connected to the internet.”.
* When login fails due to any other error, the app shows an error alert saying “Unexpected error.”.
* When login is successful, the app proceeds to the Home view.

---

As a User, I want that the application automatically extends my existing access token, when I start the application, so that I can onboard the application with a fresh authentication.

**Acceptance criteria:**
* When the app is started and it has an authentication, it extends the authentication to reset its expiry.
* The app shows a progress indication while extending the authentication.
* When extending the authentication succeeds, the app proceeds to the Home view.

----

As a User, I want that the application requires me to log in, when it fails to extend my authentication, so that I can get a new authentication.

**Acceptance criteria:**
* When extending the authentication fails, the app presents the Login view.
* When the login succeeds, the app proceeds to the Home view.

----

As a User, I want that the Home view shows authentication details so that I can see my name and role.

**Acceptance criteria:**
* The Home view shows	
    * User’s name
    * User’s role

----

#### Authentication API (<https://example.vividmindsoft.com>)

**Authenticate**

Endpoint: `/idp/api/v1/token`

Request (`POST, application/x-www-form-urlencoded`):

* `username`
* `password`
* `grant_type = “password”`
* `client_id = “69bfdce9-2c9f-4a12-aa7b-4fe15e1228dc”`

Response (`application/json`)
* `200`:
```json
{
   "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZHA6dXNlcl9pZCI6IjUwYTdkYTFkLWZlMDctNGMxNC04YjFiLTAwNzczN2Y0Nzc2MyIsImlkcDp1c2VyX25hbWUiOiJqZG9lIiwiaWRwOmZ1bGxuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiZWRpdG9yIiwiZXhwIjoxNTU2NDc2MjU1fQ.iqFmotBtfAYLplfpLVh_kPgvOIPyV7UMm-NZA06XA5I",
   "token_type": "bearer",
   "expires_in": 119,
   "refresh_token": "NTBhN2RhMWQtZmUwNy00YzE0LThiMWItMDA3NzM3ZjQ3NzYzIyNkNmQ5OTViZS1jY2IxLTQ0MGUtODM4NS1lOTkwMTEwMzBhYzA="
}
```
where
* `access_token`: the JWT token
* `refresh_token`: the token used to extend the authentication
* `expires_in`: the period (in seconds) after which the access token expires
* `token_type`: bearer (constant)

* `401`: when the authentication has failed due to invalid credentials

**Extend authentication**
Endpoint: `/idp/api/v1/token`

Request (`POST, application/x-www-form-urlencoded`):

* `refresh_token`
* `grant_type = “refresh_token”`
* `client_id = “69bfdce9-2c9f-4a12-aa7b-4fe15e1228dc”`

Response (application/json``): 
* `200`: The same as the authentication response above.
* `401`: when the operation has failed due to invalid refresh token	