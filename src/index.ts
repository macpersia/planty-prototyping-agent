// import * as fs from 'fs';

// const pwd = fs.realpath('.');
// console.log(`PWD:${pwd}`)

const yeoman = require('yeoman-environment');
const env = yeoman.createEnv();

const done = () => {
    console.log('done');
};
env.register(require.resolve('./generator-jhipster'), 'jhipster:app');
env.run('jhipster:app', { 'skip-install': true }, done);

// env.lookup(() => { 
//     env.run(
//         'jhipster', { 'skip-install': true }, 
//         (err: any) => {
//             console.log(err);
//         },
//         () => {
//             console.log('done');
//         }
//     );
// });