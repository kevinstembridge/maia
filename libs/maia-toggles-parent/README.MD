# What Are They?

Feature toggles are a way to change the behaviour of an application at runtime. They allow us to turn a new feature on and, more importantly, turn it off again if it doesn’t work correctly, without having to redeploy or bounce the application.

[Feature Toggles (aka Feature Flags)](https://martinfowler.com/articles/feature-toggles.html)

# Why?
## 'Fix' Bugs in Seconds

One of the 4 DORA Metrics, the metrics that correlate with high performing organisations, is Time To Restore Service (TTR). This is the amount of time it takes between discovering a problem in production and making the problem go away.

If we have to do a code change or even just a configuration change with an application restart, this is going to take anywhere from several minutes, to hours, or even days.

With toggles, we can ‘turn off’ bugs in a matter of seconds.

## Avoid Rollbacks
We sometimes do rollbacks to fix problems in production. Rollbacks are bad.

Rollbacks take away features that our users have requested.

Rollbacks may re-introduce bugs that were fixed in the current version.

Rollbacks might not be possible due to changes in data models.

It is far better to leave the current version in place but just turn off the bad feature, fix it and then roll out a new version.

## Reduce Merge Conflicts
We may be tempted to build features on feature branches so that production releases can go ahead prior to our feature being finished. The longer that something stays on a feature branch the more chance there is of merge conflicts (rebasing doesn’t solve this problem, it just delays it). With feature toggles, we can merge incomplete features and even safely deploy them to production in a disabled state.

## Co-ordinate Releases
Sometimes we need to make a change in collaboration with an upstream or downstream system. Instead of having to co-ordinate with other teams to synchronise our deployments, we can instead deploy on our own schedule and then just co-ordinate the turning on of the toggle.

## Gradual Rollout
Some feature toggles libraries, such as the one we use (Togglz), allow us to enable a feature based on conditions. For example, we could enable it for just a single user or subset of users, for a given IP address or for a given percentage of incoming requests.

# What Are the Downsides?

## More Execution Paths in Code

Instead of having a statement like this…

```
doSomethingTheOldWay()
```

We would have a statement like this…


```
if (MyToggle.isActive()) {
    doSomethingTheNewWay()
} else {
    doSomethingTheOldWay()
}
```

It’s not hard to understand, but it is a little more complex having more execution paths in your code.

## State Management

We need to persist the state of a toggle across application restarts. The Togglz library provides various integrations for doing this (file-based, JDBC, caches).

We need to manage the state of a toggle across environments. Beware of testing in one environment and then expecting it to behave the same in the next environment.

# How?

TODO
