import React from 'react';
import { Composition } from 'remotion';
import { TokePromo1Min } from './TokePromo1Min';

export const RemotionRoot: React.FC = () => {
  return (
    <>
      <Composition
        id="TokePromo1Min"
        component={TokePromo1Min}
        durationInFrames={1800}
        fps={30}
        width={1920}
        height={1080}
      />
    </>
  );
};
