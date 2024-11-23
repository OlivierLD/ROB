# PHP Celestial Computer
> Designed with PhP v8.  

We chose here to produce JSON data from the PHP.  
This way, it's up to the HTML page invoking the PHP (with a `fetch`) to deal with it.  
See the examples to see how.

Based on the work of Jean Meeus and [Henning Umland](https://www.celnav.de/).

- Also see [W3Schools](https://www.w3schools.com/php/default.asp)
- [How to debug PhP](https://www.google.com/search?q=how+to+debug+php&oq=how+to+debug+php&gs_lcrp=EgZjaHJvbWUyBggAEEUYOTIGCAEQRRhA0gEINDAzMmowajGoAgCwAgA&sourceid=chrome&ie=UTF-8#fpstate=ive&vld=cid:4b06443e,vid:8ka_Efpl21Y,st:0)

## To run a sample locally
```
$ php -S localhost:8000
```

Then, in a browser, load `http://localhost:8000/index.html`.

## How many code lines ?
```
$ (find . -name '*.php' -print0 | xargs -0 cat) |  wc -l
```
ou aussi 
```
$ echo -e "$((find . -name '*.php' -print0 | xargs -0 cat) |  wc -l) lignes de php code"
```

---
