document.addEventListener('DOMContentLoaded', ()=>{
  const btn = document.getElementById('logout');
  if(!btn) return;
  btn.addEventListener('click', function(){
    try{ localStorage.removeItem('fallapp_user'); }catch(e){}
    window.location.href = '../index.html';
  });
});
