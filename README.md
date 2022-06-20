# PANDD
**Promos and Discounts Info Sharing App** allows users to post discounts and promos they know about, while being able to register code numbers of the products in discount and the store they found them in. Users can then check if there's any post containing said info in their area and make use of it!

Time spent: **50** hours spent in total


https://user-images.githubusercontent.com/61529930/174683416-8fbbd310-80be-483c-87a1-d1084e5cb8b9.mp4



## Table of Contents
* Description
* App evaluation
* Required user stories
* Optional nice-to-have stories
* Screen Archetypes
* Navigation

## Description
Informative and social media app that allows users to post discounts and promos they know about in establishments, while being able to register their respective code numbers of the products in discount. Users can then check if there’s any post containing said info in their area and make use of it!

## App Evaluation
* **Category:** Promos & discounts / Social
* **Mobile:** Uses camera to scan barcodes of products, mobile-only experience.
* **Market:** Anyone that do shopping regularly could make use of this app. Ability to filter promos by distance, allowing users to interact with relevant content.
* **Habit:** Users can search promos and discounts any time during the day, and may post their ones when they are shopping.
* **Scope:** Posting and searching promos and discounts. Future posibility of implementing shopping lists, prices comparison, etc.

## Required Must-have Stories

* [X] User can post promos and discounts including name and address of the store
* [X] User can scan barcodes of products
* [X] User can create a new account
* [X] User can login
* [X] User can search for other promos and discounts based on store address
* [X] User can view on a map stores with discounts posts on them

## Optional Nice-to-have Stories

* [ ] User can mark as useful or useless promos and discounts posts
* [X] User can set promos and discounts expiring dates in their posts
* [X] User can set their profiles with a profile photo
* [X] User can add an image to their posts
* [ ] User can follow/unfollow other users
* [ ] User can see a list of their followers and a list of their following
* [X] User can search by product name

## Additional stories

* [X] User can subscribe to a store and get notifications whenever a new post is made in that store

## Screen Archetypes

* Login screen
   * User can login
* Registration screen
   * User can create a new account
* Home
   * User can scroll down and search for other promos and discounts based on store address and/or barcodes of products
* Map screen
   * User can view on a map stores with discounts posts on them
* Camera scanner
   * User can scan barcodes of products
* Post
   * User can post promos and discounts including name and address of the store

## Navigation

### Tab Navigation (Tab to Screen)

* Home
* Map screen
* Camera screen

### Flow Navigation (Screen to Screen)

* Login screen
   * → Home
* Registration screen
   * → Home
* Home
   * → Post screen
* Map screen
   * → None
* Camera scanner
   * → None
* Post
   * → None

### Wireframes
https://www.figma.com/proto/2zmMJ4Z2jR1lm8eUF2V7bS/Untitled?node-id=1%3A2&scaling=scale-down&page-id=0%3A1

### Models
Post
 | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | createdAt     | DateTime | date when post is created (default field) |
   | user        | Pointer to User| post author |
   | description       | String   | description by author |
   | barcode     | String | product barcode |
   |store   | Pointer to Store | Store mentioned | 
   | image         | File     | image that user posts |
   | promo expire date |DateTime| date when the promo ends|
   
Store
| Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | createdAt     | DateTime | date when post is created (default field) |
   | name     | String | store name |
   | address   | String | store address |
   | mapId   | String | google map Id |
   | long   | Number | store address longitude |
   | lat   | Number | store address latitude |
   
   

User
 | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user (default field) |
   | createdAt     | DateTime | date when post is created (default field) |
   | username     | string | users username |
   | password     | string | users passwords |
   | email     | string | users email |
   | profile         | File     | image that user posts |
   
### Network Request Outline
* Login screen
   * (Read/GET) Check if user account exists.
* Registration screen
   * (Create/POST) Create a new user account.
* Home 
    * (Read/GET) Query all posts and sort them by created date.
* Map screen
    * (Read/GET) Query all stores.
* Camera scanner
    *  None
* Post
    * (Create/POST) Create a new post.
    
