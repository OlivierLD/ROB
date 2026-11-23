/*
 * Misc dummy tests
 */

const d = new Date();

let year    = d.getUTCFullYear();
let month   = d.getUTCMonth();
let day     = d.getUTCDate();
let hours   = d.getUTCHours();
let minutes = d.getUTCMinutes();
let seconds = d.getUTCSeconds();

console.log(`UTC: ${year}-${month + 1}-${day} ${hours}:${minutes}:${seconds}`)