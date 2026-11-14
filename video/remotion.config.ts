import { Config } from '@remotion/cli/config';

Config.setVideoImageFormat('jpeg');
Config.setOverwriteOutput(true);
if (process.platform === 'win32') {
  Config.setChromiumOpenGlRenderer('angle');
}
