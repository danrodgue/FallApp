document.addEventListener('DOMContentLoaded', ()=>{
  const btn = document.getElementById('logout');
  if(!btn) return;
  btn.addEventListener('click', function(){
    try{ localStorage.removeItem('fallapp_user'); }catch(e){}
    // Volver a la pantalla de login ubicada en js/index.html
    window.location.href = '../js/index.html';
  });
});
