# Shop API

## Final project for "Web Development with Java and Spring" course

### Starting the app

Before starting the app you need to start docker containers with `docker-compose up -d` command run in the terminal.
After this step app is ready to start.

There are two profiles:

- `local` with H2 embedded database (but required docker container for redis)
- `dev` with PostgreSQL database in docker container

To restart data in `dev` profile run `docker-compose down -v` and then `docker-compose up -d` again.

### Documentation

OpenAPI documentation can be found in `docs` folder, along with entity diagram and demo Postman collection.

### Requirements

- Register new user. Example request: {“email”:”my@email.com”, “password”:”123”} Respond with an appropriate HTTP
  codes (200 for ok, 409 for existing user) Your app must not store password as plain text, use some good approach to
  identify user.
- Login into system. Example request: {“email”:”my@email.com”, “password”:”123”} Respond with JSON containing sessionId.
  Optional: think about preventing an intruder from bruteforcing.
- (optional) Reset password.
- Get all products in store.Respond with JSON list of items you have, e.g.: {“id”:”2411”, “title”:”Nail gun”,
  “available”:8, “price”: “23.95”}
- Add item to cart. Example request: {“id”:”363”, “quantity”:”2”} Allow adding only one position at a time. If you don’t
  have this quantity in store - respond with an error. The information has to be session-scoped: once session expires -
  user will get new empty cart.
- Display your cart content. Respond with list of product names with their quantities added. Calculate subtotal. Assign
  an ordinal to each cart item.
- Remove an item from user’s cart.
- Modify cart item. Example request: {“id”:2, quantity: 3} - user should be able to modify number of some items in his
  cart.
- Checkout: verify your prices in cart, ensure you still have desired amount of goods. If all is good - send a user
  confirmation about successful order.
- (optional) Cancel order: return all products from order back to available status.
- (optional) Get user’s order list. Should contain order id, date, total, status.