# Make sampling a first class citizen _(working title)_


We want to make sampling a first class citizen rather than a side-effect of the first aggregation.
This will address a number of problems:

* Users are often confused about the raw unsampled data, and forgetting to add a down-sampling
  aggregation might lead to invalid results and conclusions.


* **Status** Draft
* **Author** Martin Parm <parmus@spotify.com>   
* **PR** https://github.com/spotify/heroic/pull/225


## Suggestion

User can provide an optional sampling configuration with their query.


```json
{
  "samplingMethod": "average"
}
```

Heroic will pick a default sampling, if no sampling configuration is provided.
The query result will contain a similar structure to inform the user about, which sampling methods
was used in the query.

### Sampling methods

_(To be written)_

### Predictable on memory consumption

Sampling can be applied while loading data points from the backend and the resulting number of data
points will be upper-bound by _number_of_timeseries_ * (_time_range_ / _resolution_), thus making
the memory consumption very predictable.
This upper bound can even by calculated using just the metadata, making it possible to reject
heavy queries without loading any data from the backend.

The same predictability also makes it possible to implement an auto-resolution feature with a similar
predictable memory comsumption. However this is will be addressed in a different RFC.


## Extensions

This section contains optional extensions to this RFC, which could provide further benefits.


### Up-sampling

_(To be written)_

### Simplifying the aggregation engine

The aggregation engine currently have to support an unknown number of data points per bucket in
every step due to the unpredictable nature of the initial unsampled data.
With the more predictable natue of sampled data, the aggregation engine can assume that each time
series always have exactly 0 or 1 data point per bucket.

The aggregation engine also currently deals with resolution at every aggregation step, however we
have observed very little practical use of this feature. As sampling will effective deal with
resolution before the data points reach the aggregation engine, we can remove this complication
from the aggregation engine completely.

These two changes should result in a much simpler aggregation engine with more predictable run-time
behavior and memory consumption.  
