### Current idea:
1. Bot receives location (e.g. 'Sofia, Bulgaria') and day marker (e.g. 'tomorrow' or number of a day in current month)
   1. ?Store user location in DB?
   2. ?Today is 21st of February, should we count day marker = 1 as 1st of March?
2. Convert the location to coordinates (as API requires)
3. Bot retrieve weather info from the site and send it to some LLM
   1. ?Chatbot? Are they available for free with API?
4. The question for LLM is "What do you recommend to do in that day and that weather?"
5. Return the answer to the user