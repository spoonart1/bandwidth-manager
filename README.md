# Bandwith Manager (Kotlin)

## An Utility to check connection speed

### Requirement
#### we need this library to request data (in this case -> request image data)
```compile 'com.squareup.okhttp3:okhttp:3.3.0'```

### Usage
``` 
BandwidthManager(object : BandwidthManager.BandWithListener {
            override fun onNetworkPoorDetected() {
                // poor connection
            }

            override fun onNetworkStable() {
                // stable connection
            }
        })
```

### Listener
```
-> BandwidthManager.BandWithListener
interface BandWithListener {
        fun onNetworkPoorDetected()
        fun onNetworkStable() {} //optional
        fun onNetworkFailed() {} //optional
    }
```


# Free to use!
