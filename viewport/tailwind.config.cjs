/** @type {import('tailwindcss').Config} */
module.exports = {
    mode: 'jit',
    content: [
      "./index.html",
      "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            fontFamily: {
                forma: ["forma-djr-display", 'Arial', 'Helvetica', 'sans-serif'],
                darkmode: ["darkmode-off", 'Arial', 'Helvetica', 'sans-serif'],
            },
            fontSize: {
              '2xs': '0.625rem',
              'mega': '15rem'
            },
            spacing: {
              '1px': '0.063rem',
              '2px': '0.125rem',
              '3px': '0.188rem',
              '4px': '0.25rem',
              '5px': '0.313rem',
              '6px': '0.375rem',
              '7px': '0.438rem',
              '8px': '0.5rem',
              '9px': '0.563rem',
              '10px': '0.625rem',

              '11px': '0.688rem',
              '12px': '0.75rem',
              '13px': '0.813rem',
              '14px': '0.875rem',
              '15px': '0.938rem',
              '16px': '1rem',
              '17px': '1.063rem',
              '18px': '1.125rem',
              '19px': '1.188rem',


              '24px': '1.5rem',
              '25px': '1.563rem',
              '50px': '3.125rem',
              '75px': '4.688rem',
              '85px': '5.313rem',

              '20px': '1.25rem',
              '30px': '1.875rem',
              '40px': '2.5rem',
              '47px': '2.938rem',

              '60px': '3.75rem',
              '70px': '4.375rem',
              '80px': '5rem',
              '90px': '5.625rem',

              '100px': '6.25rem',
              '150px': '9.375rem',

              '200px': '12.5rem',
              '250px': '15.625rem',
              '300px': '18.75rem',
              '350px': '21.875rem',
              '400px': '25rem',
              '450px': '28.125rem',
              '500px': '31.25rem',

              '550px': '34.375rem',
              '600px': '37.5rem',
              '650px': '40.625rem',
              '700px': '43.75rem',
              '750px': '46.875rem',
              '800px': '50rem',
              '850px': '53.125rem',
              '900px': '56.25rem',
              '950px': '59.375rem',
              '1000px': '62.5rem',

              // Overly Specific Items:\
              '125px': '7.813rem',
              '124px': '7.75rem',
              '182px' : '11.375',
            },
            minHeight: {
                '4/6': '66.666667%',
            },
            colors: {
                transparent: 'transparent',
                current: 'currentColor',
                'template-white': '#FEFEFE',
                'template-gray': '#202124',
                'template-black': '#0f0f0f',
              // ...
            },
            opacity: {
                '1': '0.01',
                '2': '0.02',
                '3': '0.03',
                '4': '0.04',

                '6': '0.06',
                '7': '0.07',
                '8': '0.08',
                '9': '0.09',
              // ...
            },
            boxShadow: {
                'xl': '0 10px 50px 10px rgb(0 0 0 / 0.5)',
                '2xl': '0 10px 50px 10px rgb(0 0 0 / 0.75)',

                'inset-sm': '0px 0px 7px -1px rgba(0,0,0,0.5) inset',
                'inset': '0px 0px 15px -1px rgba(0,0,0,0.5) inset',
                'inset-md': '0px 0px 20px -1px rgba(0,0,0,0.5) inset',
                'inset-lg': '0px 0px 50px -1px rgba(0,0,0,0.6) inset',
                'inset-xl': '0px 0px 50px -1px rgba(0,0,0,0.75) inset',
                'white-inset-sm': '0px 0px 7px -1px rgba(255,255,255,0.5) inset',
                'white-inset': '0px 0px 15px -1px rgba(255,255,255,0.5) inset',
                'white-inset-md': '0px 0px 20px -1px rgba(255,255,255,0.5) inset',
                'white-inset-lg': '0px 0px 50px -1px rgba(255,255,255,0.6) inset',
                'white-inset-xl': '0px 0px 50px -1px rgba(255,255,255,0.75) inset',

                'white-md': '0px 0px 20px -1px rgba(255,255,255,0.3)',
            }
        }
    },
    plugins: [],
  }