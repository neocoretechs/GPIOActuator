<h2>GPIOActuator</h2>
Why does a chicken dance its food into the dirt? My theory is that it makes it hard for her mortal enemy, the rat, to easily steal it. 
Consider: anywhere a chicken can go, the rat can follow. Place a mound of food or any configuration of feed at the hens disposal, the rat
can easily acquire it. The exception being spread throughout the dirt. The chicken can then peck it incrementally, something they are
optimized for, while the rat is flummoxed due to its ratty configuration.<p/>
In that vein the optimum chicken feeder is one that dumps the feed into the dirt on a timed basis; hence the genesis of this project. This code
activated a series of solenoids (or any gpio device), once a day over 6 days to open a latch that causes a door on a series
of compartments of an upside down box filled with feed to swing down, thus dumping the chickenfeed.<p/>
Of course, any timed GPIO operation can also be performed via Java using an SBC. The code here also has the advantage of
recording the last solenoid activated such that if a failure occurs and a restart is performed, the next latch in the
sequence is properly activated. The code does not depend on an RTC of the correct time, merely sleeps the necessary interval
between operations. The process should be started 24 hrs before the time the first feeding is to occur. If a power fail occurs, the bird will have to wait
the interval the power returns plus 24 hours, so hopefully the hungry, angry hen(s) only wait a maximum of 2 days for their feed in that case.
