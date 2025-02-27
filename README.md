# WhatToDoBot

## Current idea:

1. Bot receives location (city name, e.g. 'Sofia') and day marker (e.g. 'tomorrow' or number of a day as offset from
   today)
   1. ?Store user location in DB?
2. If there is no coordinates for the location, use old API and save the coordinates. Otherwise, use the coordinates.
3. Bot retrieve weather info from the site and send it to some LLM
   1. ?Chatbot? Are they available for free with API?
4. The question for LLM is "What do you recommend to do in that day and that weather?"
5. Return the answer to the user

## Setup

- Refer to [local-run.md](docs/local-run.md) for instructions on how to run the bot locally.