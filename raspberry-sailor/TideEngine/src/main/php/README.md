# PHP Tide Computer

Work in progress.  
Here, size does matter.  
`php` array representation of the stations is too big (fails in memory allocation).  
Same when reading the corresponding JSON file.  

Trying `sqlite`... That seems to work. This requires the DB to exist (`sql/tides.db`).

More soon.

See:
- <https://www.php.net/manual/en/sqlite3.query.php>
- <https://www.php.net/manual/en/book.sqlite3.php>


## A test
From the `php` folder, start the server:  
```
$ php -S localhost:8000
```
Then from a browser, reach `http://localhost:8000/tide.sample.php`.  
Or from a terminal, do a 
```
$ curl http://localhost:8000/tide.sample.php
```


---
