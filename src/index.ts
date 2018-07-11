var yeoman = require('yeoman-environment');
var env = yeoman.createEnv();

// env.register(require.resolve('generator-jhipster'), 'jhipster:app');
// env.run('jhipster:app', {}, done);
// // done.run('jhipster:app entity', {}, done);

env.lookup(() => { 
    env.run(
        'jhipster', { 'skip-install': true }, 
        (err: any) => {
            console.log(err);
        },
        () => {
            console.log('done');
        }
    );
});