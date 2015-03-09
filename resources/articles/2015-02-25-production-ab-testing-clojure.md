---
title: Production A/B Testing in Clojure
author: Charles-Philippe Clermont
headline: Production A/B Testing in Clojure
headline-desc: Part I - Setup, identities and cohorts
scripts:
  - "/js/highlight.pack.js"
  - source: hljs.initHighlightingOnLoad();
stylesheets:
  - "/css/solarized_light.css"
---

<div class="summary">
**What you'll learn:**

* How to start A/B testing in your Clojure web app,
* How to deal with identities,
* How to deal with cohorts.

</div>
<section>
## Context

I inherited Bestcase, a Clojure A/B testing library.
Search "clojure ab testing", two results:

* [Touchstone][ts], a fire-and-forget A/B testing library;
* [Bestcase][bc], a traditional A/B testing library.

The makers of Touchstone argue that traditional A/B testing is a nuisance to track.
That might be true.
I've read about the benefits of the fire-and-forget approach.
They are mathematically more efficient.
Somehow, I still believe the insights you gain from traditional split testing outweigh efficiency.
I'm all about delayed gratification, ya know.

Anyways...

Figured I should setup Bestcase on my personal site.
(It's all Clojure.)
Dependencies were outdated.
Time for an upgrade!
Submit pull request.
Inherit the whole project...
Whoa!
OK.

So I installed it on my site.
Fixed some bugs here and there.
Faced a bunch of road blocks.
I hope this article can help you.
</section>
<section>
## Getting Started

Open your favorite clojure project's `project.clj` and add the following dependency:

    :dependencies [[com.cpclermont/bestcase "0.2.1"]]

Next, you'll need to initialize the store.
You can use an in-memory store or a Redis instance.
Assuming you want your results to persist, you'll want a Redis instance.

Setting it up is pretty straightforward.

```clojure
(ns example.core
  (:require [bestcase.core :refer [set-config! with-identity alt score]
            [bestcase.store.redis :refer [create-redis-store]))

(def redis-conn-opts {:spec {:uri "redis://..."}})

(set-config! {:store (create-redis-store redis-conn-opts)})
```

Setting up a test is also straightforward.
You need two things: An identity and a test.

```clojure
(with-identity "alice"
  (alt :mailchimp-headline
    :control "Like this post?"
    :alternative-1 "Want more?"
    :alternative-2 "Cool story, bro."))
```

Similarly for tracking conversions

```clojure
(with-identity "alice"
  (score :mailchimp-headline :goal-name))
```

And that's all there is to it.
</section>
<section>
## Dealing with identities

The identity of the testee is an important issue in A/B testing.
Here's why:

Alice is a human being, not a cookie, nor a session, nor an email address or a sequence of characters.
She's a human being.
If she's being A/B tested, she should have a consistent experience across her devices.
In sensitive situations, her phone, work computer and home computer should all say the same thing.

In non-sensitive situations, don't worry so much.
The page should still say the same thing if she refreshes.
In that case, the simple approach is to store the identity in the session.
Don't worry too much about it, Bestcase comes with two helpers for this.
The first is
 [bestcase.util.ring/default-identity-fn](http://charlespwd.github.io/bestcase/bestcase.util.ring.html#var-default-identity-fn)
 , which looks for or assigns a unique identifier in the cookie.
The second is
 [bestcase.util.ring/identity-middleware-wrapper](http://charlespwd.github.io/bestcase/bestcase.util.ring.html#var-default-identity-fn)
 , which generates ring middleware from identity functions.

In a web app, it would look like this (it's an edited version of what I'm using for this site):

```clojure
(ns example.web
  (:require [bestcase.util.ring :refer [identity-middleware-wrapper default-identity-fn]]
            [environ.core :refer [env]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]
            [ring.middleware.cookies :refer [cookie-store]]))

(defn wrap-app [app]
  (let [store (cookie-store {:key (env :session-secret)})
        session-config {:flash true, :store store, :cookies-attrs {:max-age (* 60 60 24 30)}}
        middleware-config (assoc site-defaults :session session-config)]
    (-> app
        ;; ... truncated for clarity

        ;; ab testing identity middleware
        ((identity-middleware-wrapper
           default-identity-fn
           {:simple-no-bots true})) ; filter bots from tests

        ;; needs to be last (!!!)
        (wrap-defaults middleware-config)))
```

What if it you want to persist the identity through registration?
You swap the default identity function for your own.
The [Keeping Track of Identity][ug] section of the User Guide contains an example.

```clojure
(defn create-new-user
  [request username]
  (let [user (db/create-new-user username)
        bestcase-id (let-if [b-id (get-in request [:session :bestcase])] b-id (str (UUID/randomUUID)))]
    (db/update-user-bestcase-id (:user-id user) bestcase-id)
    user))

(defn your-custom-identity-fn
  [request]
  (let-if [user (get-user-from-request request)]
    (:bestcase-id user)
    (let-if [b-id (get-in request [:session :bestcase])] b-id (str (UUID/randomUUID)))))
```
</section>
<section>
## Cohorts

Tracking cohorts is straightforward.
Hypothetically, let's just define a simple sales funnel as four steps.

1. `:visitor`, the user lands on your site;
2. `:sign-up`, the user signs up to your newsletter;
3. `:trial`, the user sign ups for your free trial;
4. `:purchase`, the user purchases.

I like to use a simple end point for these kinds of tests.
For instance, how do you track a MailChimp sign-up?
They're leaving your site, right?
They're no longer in your control.

Easy fix, intercept the form submission, POST to a custom endpoint with jQuery, and resume the form submission.
For this, I use a simple route

```clojure
(POST "/bingo/:test-id/:goal" [test-id goal]
  (do (bc/score (keyword test-id) (keyword goal))
      {:status 201})
```

And right under my MailChimp form, I include the following snippet

```html
<script>
$('#mc-embedded-subscribe-form').submit(function(e) {
  e.preventDefault(); // intercept submit
  var form = this;
  $.post('/bingo/mailchimp-subject-test/signup')
  .always(function() {
    form.submit(); // do it anyway
  });
});
</script>
```

Annnd... We're done :-).

What about tracking trial sign-up?
A simple one liner:

```clojure
  (bc/score :test-name :trial)
```

Annnd... We're done :-)!

What about tracking a purchase?

```clojure
  (bc/score :test-name :purchase)
```

I think you got the idea.

</section>
<section>
## Conclusion

So far, we've seen how to integrate A/B testing in your Clojure web app, setup alternatives and track their results.
In part II, I'll discuss how to end A/B tests, visualize the results, and test them in your browser.

Don't hesitate to send me an email at charles@cpclermont.com for questions.
</section>
[ts]: https://github.com/ptaoussanis/touchstone
[bc]: https://github.com/charlespwd/bestcase/
[ug]: https://github.com/charlespwd/bestcase/wiki/User-Guide#keeping-track-of-identity
[kz]: http://www.kalzumeus.com
