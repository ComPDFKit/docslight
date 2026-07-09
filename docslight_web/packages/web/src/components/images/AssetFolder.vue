<template>
  <svg width="112" height="90" viewBox="0 0 112 90" fill="none" xmlns="http://www.w3.org/2000/svg">
    <g filter="url(#asset_folder_shadow)">
      <path d="M14 25.6C14 20.8504 17.8504 17 22.6 17H43.101C45.0377 17 46.9178 17.6533 48.4373 18.8543L54.1862 23.3989C55.7057 24.5998 57.5858 25.2532 59.5225 25.2532H91.4C96.1496 25.2532 100 29.1036 100 33.8532V63.4C100 68.1496 96.1496 72 91.4 72H22.6C17.8504 72 14 68.1496 14 63.4V25.6Z" fill="url(#asset_folder_back)"/>
      <path d="M18 35.6C18 30.8504 21.8504 27 26.6 27H91.4C96.1496 27 100 30.8504 100 35.6V66.4C100 71.1496 96.1496 75 91.4 75H26.6C21.8504 75 18 71.1496 18 66.4V35.6Z" fill="url(#asset_folder_front)"/>
      <path d="M18 41H100V66.4C100 71.1496 96.1496 75 91.4 75H26.6C21.8504 75 18 71.1496 18 66.4V41Z" fill="url(#asset_folder_glass)"/>
      <path d="M28 36H89" stroke="white" stroke-opacity="0.65" stroke-width="2" stroke-linecap="round"/>
      <path d="M30 49H82" stroke="#7EC2FF" stroke-opacity="0.7" stroke-width="2" stroke-linecap="round"/>
      <path d="M30 57H75" stroke="#62AEFF" stroke-opacity="0.7" stroke-width="2" stroke-linecap="round"/>
      <path d="M30 65H68" stroke="#3F8CFF" stroke-opacity="0.65" stroke-width="2" stroke-linecap="round"/>
    </g>
    <defs>
      <filter id="asset_folder_shadow" x="0" y="4" width="112" height="86" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
        <feFlood flood-opacity="0" result="BackgroundImageFix"/>
        <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
        <feOffset dx="-2" dy="3"/>
        <feGaussianBlur stdDeviation="6"/>
        <feComposite in2="hardAlpha" operator="out"/>
        <feColorMatrix type="matrix" values="0 0 0 0 0.043 0 0 0 0 0.333 0 0 0 0 0.8 0 0 0 0.2 0"/>
        <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow"/>
        <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow" result="shape"/>
      </filter>
      <linearGradient id="asset_folder_back" x1="25" y1="16" x2="86" y2="75" gradientUnits="userSpaceOnUse">
        <stop stop-color="#D8EEFF"/>
        <stop offset="1" stop-color="#72BBFF"/>
      </linearGradient>
      <linearGradient id="asset_folder_front" x1="24" y1="27" x2="93" y2="76" gradientUnits="userSpaceOnUse">
        <stop stop-color="#CDEBFF" stop-opacity="0.92"/>
        <stop offset="1" stop-color="#1C86FF" stop-opacity="0.9"/>
      </linearGradient>
      <linearGradient id="asset_folder_glass" x1="23" y1="42" x2="100" y2="76" gradientUnits="userSpaceOnUse">
        <stop stop-color="#8FD2FF" stop-opacity="0.25"/>
        <stop offset="0.55" stop-color="#1677FF" stop-opacity="0.72"/>
        <stop offset="1" stop-color="#005CE6" stop-opacity="0.84"/>
      </linearGradient>
    </defs>
  </svg>
</template>
