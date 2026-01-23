# HytaleUsers
Small Java project that can be used to locate all available **Hytale usernames**.

## Usage

`java -jar HytaleUsers-1.1.1.jar [params]`

## Parameters

`-t, --threads <n>`

Number of concurrent threads (default: 1)

`-m, --max <n>`

Maximum username length to check (default: 3)

`-d, --delay <ms>`

Delay between batches in milliseconds (default: 5000)

`-p, --print`

Print raw API responses

`-a, --analysis`

Perform analysis on found usernames

`-h, --help`

Show help message

## Setup

A valid session cookie is required.

1. Log in to Hytale.com (you must have an account already)

2. Go to this [link](https://accounts.hytale.com/api/account/username-reservations/availability?username=aaa)

3. Open Chrome Dev Tools -> Network Tab

4. Reload the page

5. Find the availability request

6. Scroll down to the cookie section and find the cookie:

`ory_kratos_session=XXXXXXXXXX;`

7. Place your cookie value in:

data/cookie.txt

Paste only the cookie value

Correct:

`abcd1234efgh5678aslanb=`

Incorrect:

`ory_kratos_session=abcd1234efgh5678aslanb=;`

## Files & Output

`data/cookie.txt`
Cloudflare session cookie

`data/checkpoint.txt`
Last processed username (auto-resume)

`data/available_usernames.txt`
Discovered available usernames

## Behavior

- Automatically resumes from the last checkpoint  
- Writes available usernames to disk as they are found  
- Exits immediately on Cloudflare rate limit (Error 1015)  
- Uses configurable batching and rate-limiting to avoid bans  

## Disclaimer

This tool interacts with a protected endpoint.  
Use responsibly and respect Hytale / Cloudflare rate limits.