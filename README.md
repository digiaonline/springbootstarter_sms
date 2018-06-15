
starcut.auth.sms
Properties: 

```starcut.auth.sms.shortCode``` 

Dedicated short code for the application if any


```starcut.auth.sms.codeLength```

The length of the verification code sent by SMS. The default value is 6.


```starcut.auth.sms.maxSmsPerPeriod```

The maximum number of SMS that can be sent for a number during the last "periodInMinutes" minutes.


```starcut.auth.sms.periodInMinutes```

The duration in minutes of the period on which there cannot be more than "maxSmsPerPeriod" SMS sent to the same phone number.


```starcut.auth.sms.codeValidityInMinutes```

The lifetime in minutes of an SMS code.


```starcut.auth.sms.maxTrialsPerCode```

The maximum number of trials of one code before it gets disabled.


```starcut.auth.sms.region```

The default region for parsing phone numbers. Default is "FI".


```starcut.auth.sms.allowedRegions```

Restricts the valid phonenumbers to the ones belonging to the list of regions.
For instance, "FI,FR" would allow sending SMS only to Finnish or French numbers,
other numbers would be considered as invalid.

The default or empty list means no restriction, i.e., phone numbers of all countries are valid.


```starcut.auth.sms.senderId```

The sender id which appears in the SMS. Default value is "Starcut"

This is not supported in all countries.


```starcut.auth.sms.minTimeBetweenTwoSmsInSeconds```

The number of seconds required before requesting a new SMS emission. The default and minimum is 1.


