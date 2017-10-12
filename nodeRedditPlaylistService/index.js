'use strict';
const express = require('express');
const snoowrap = require('snoowrap');
const bodyParser = require('body-parser');
const googleImages = require('google-images')

const server = express();
const giClient = new googleImages("009405977754643700996:ps9jkrfuuww", "AIzaSyBUcRSXRY75DSwcpqeBN4eTrT89ExOxQLY");

server.use(bodyParser.urlencoded({ extended: true }));
server.use(bodyParser.json());

const port = process.env.PORT || 8080;
const router = express.Router(); 

router.get('/music', function(req, res) {
    toUrls(playableUrls(getmusic())).then(data => res.json(data));
});

router.get('/googleImages/:query', function(req, res){
    giClient.search(req.params.query).then(images => {
        let urls = images.map(image => image.url)
        res.json(urls)
    })

})

router.get(`/:subreddit`, function(req, res){
    toTitleUrls(getTop(req.params.subreddit)).then(data => res.json(data));
});

server.use('/reddit', router);
server.listen(port);
console.log(`dankest songs hosted on ${port}`);

const reddit = new snoowrap({
    userAgent: `gets reddit musc`,
    clientId: `WHYRDaE-C72QtQ`,
    clientSecret: `Yi0ILOTLfd0bGLeTk6ggbOmH-tI`,
    username: `Chipay`,
    password: `ab5820` 
});

const playableUrls = listings => listings
                            .filter(submission => submission.domain.startsWith('youtu') || submission.domain.startsWith('soundcloud'));

const toUrls = listings => listings.map(submission => submission.url);

const toTitleUrls = listings => listings.map(submission => ({url:submission.url, title: submission.title}));

const getmusic  = _ => reddit.getSubreddit(`Music`).getTop({limit: 100, time: 'week'});
const getPH     = _ => reddit.getSubreddit(`programmerhumor`).getTop({limit: 60, time: 'month'});
const getTop    = subreddit => reddit.getSubreddit(subreddit).getTop({limit: 50, time: 'week'});