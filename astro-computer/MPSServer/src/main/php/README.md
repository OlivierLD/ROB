# A PHP REST Server

### WiP, PoC...

Inspired by https://medium.com/@dharshithasrimal/php-rest-api-7441197312d7


From the `src/main/php` folder, start php:
```
$ php -S localhost:8000
```

Then, from a REST client, (`curl`, Postman, etc):
```
$ curl -X POST http://localhost:8000/REST.MPS.php -d '{ "akeu": "coucou" }'
Method is [POST]
{"message":"User created successfully","input":{"akeu":"coucou"}}
```

This will work...  
Will be moved to `AstroComputer/arc/main/php.v7/astro`.