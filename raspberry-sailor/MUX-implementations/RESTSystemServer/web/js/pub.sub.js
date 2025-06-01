/**
 Subscribe like this:
 <code>
 events.subscribe('topic', function(val) {
   doSomethingSmart(val);
 });
 </code>
 Publish like that:
 <code>
 events.publish('topic', val);
 </code>
 */
let events = {
    listener: [],

    subscribe: function (topic, action) {
        this.listener.push({
            'topic': topic,
            'actionListener': action
        });
    },

    publish: function (topic, value) {
        for (let i = 0; i < this.listener.length; i++) { // TODO forEach ?
            if (this.listener[i].topic === topic) {
                this.listener[i].actionListener(value);
            }
        }
    }
};
