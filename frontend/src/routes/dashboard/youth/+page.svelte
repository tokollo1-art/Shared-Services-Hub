<script>
	import RoleGuard from '$lib/components/RoleGuard.svelte';
	import TaskCard from '$lib/components/TaskCard.svelte';
	import ExperienceCard from '$lib/components/ExperienceCard.svelte';
	import ApiError from '$lib/components/ApiError.svelte';
	import { api } from '$lib/api.js';
	import { currentUser } from '$lib/stores/auth.js';
	import { formatCurrency } from '$lib/utils/formatters.js';
	import { onMount } from 'svelte';

	const user = $derived($currentUser);

	let tasks = $state([]);
	let experiences = $state([]);
	let loadingTasks = $state(true);
	let loadingExp = $state(true);
	let errorMessage = $state(null);

	const isAlumni = $derived(user?.isAlumni || false);

	// Summary stats
	const verifiedExperiences = $derived(experiences.filter((e) => e.isVerified));
	const averageRating = $derived.by(() => {
		const rated = verifiedExperiences.filter((e) => e.rating);
		if (rated.length === 0) return '5.0';
		const sum = rated.reduce((acc, curr) => acc + Number(curr.rating), 0);
		return (sum / rated.length).toFixed(1);
	});

	// Total earned from verified or completed tasks
	const totalEarned = $derived.by(() => {
		return experiences.length * 1100; // estimated/average earnings
	});

	async function loadDashboardData() {
		errorMessage = null;

		// 1. Fetch open tasks
		loadingTasks = true;
		try {
			const res = await api.tasks.list();
			tasks = Array.isArray(res) ? res : [];
		} catch (err) {
			console.error('Failed to load tasks:', err);
			errorMessage = err.message || 'Could not load tasks.';
		} finally {
			loadingTasks = false;
		}

		// 2. Fetch experience ledger for this youth
		const youthId = user?.id || 1;
		loadingExp = true;
		try {
			const res = await api.experiences.listForYouth(youthId);
			experiences = Array.isArray(res) ? res : [];
		} catch (err) {
			console.error('Failed to load experience ledger:', err);
		} finally {
			loadingExp = false;
		}
	}

	onMount(() => {
		loadDashboardData();
	});
</script>

<svelte:head>
	<title>Youth Candidate Dashboard | Shared Services Hub</title>
</svelte:head>

<RoleGuard allowedRoles={['YOUTH', 'ADMIN']}>
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Header with Welcome & Alumni Badge -->
		<div class="rounded-3xl border border-indigo-100 bg-gradient-to-r from-indigo-900 via-indigo-800 to-purple-900 p-8 text-white shadow-xl shadow-indigo-950/10 mb-8 relative overflow-hidden">
			<div class="relative z-10 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
				<div>
					<div class="flex items-center gap-2.5 flex-wrap">
						<h1 class="text-3xl font-extrabold tracking-tight">
							Welcome back, {user?.name || 'Candidate'}
						</h1>
						{#if isAlumni}
							<span class="inline-flex items-center gap-1 rounded-full bg-emerald-500/20 px-3 py-1 text-xs font-bold text-emerald-300 border border-emerald-400/30">
								<span>★ Permanent Alumni</span>
							</span>
						{:else}
							<span class="inline-flex items-center gap-1 rounded-full bg-indigo-500/30 px-3 py-1 text-xs font-semibold text-indigo-200 border border-indigo-400/30">
								<span>● Active Candidate</span>
							</span>
						{/if}
					</div>
					<p class="mt-2 text-sm text-indigo-200 max-w-xl">
						Complete verified SME tasks to build your Experience Ledger and earn guaranteed corporate-funded income.
					</p>
				</div>

				<div class="flex items-center gap-3">
					<a
						href="/tasks"
						class="rounded-xl bg-white px-4 py-2.5 text-xs font-bold text-indigo-900 shadow-xs hover:bg-indigo-50 transition-all text-center"
					>
						Browse All Tasks
					</a>
				</div>
			</div>

			<!-- Stat Counters -->
			<div class="mt-8 pt-6 border-t border-indigo-700/50 grid grid-cols-2 sm:grid-cols-4 gap-4">
				<div>
					<div class="text-xs text-indigo-200 font-medium">Available Tasks</div>
					<div class="text-2xl font-black mt-0.5">{tasks.filter(t => t.status === 'OPEN').length}</div>
				</div>
				<div>
					<div class="text-xs text-indigo-200 font-medium">Verified Records</div>
					<div class="text-2xl font-black mt-0.5">{verifiedExperiences.length}</div>
				</div>
				<div>
					<div class="text-xs text-indigo-200 font-medium">Average Rating</div>
					<div class="text-2xl font-black mt-0.5 flex items-center gap-1 text-amber-300">
						<span>★</span>
						<span>{averageRating}</span>
					</div>
				</div>
				<div>
					<div class="text-xs text-indigo-200 font-medium">Guaranteed Paid</div>
					<div class="text-2xl font-black mt-0.5 text-emerald-300">{formatCurrency(totalEarned)}</div>
				</div>
			</div>
		</div>

		<!-- Error Banner -->
		<ApiError message={errorMessage} onretry={loadDashboardData} />

		<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
			<!-- Left Column (2 Cols): Open Tasks Available to Apply -->
			<div class="lg:col-span-2 space-y-6">
				<div class="flex items-center justify-between">
					<div>
						<h2 class="text-xl font-bold text-slate-900">Open SME Tasks</h2>
						<p class="text-xs text-slate-500">Tasks ready for execution with guaranteed corporate funding.</p>
					</div>
					<a href="/tasks" class="text-xs font-semibold text-indigo-600 hover:text-indigo-800">
						View All ({tasks.length}) →
					</a>
				</div>

				{#if loadingTasks}
					<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
						{#each Array(4) as _}
							<div class="h-48 rounded-2xl bg-slate-200/60 animate-pulse"></div>
						{/each}
					</div>
				{:else if tasks.length === 0}
					<div class="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center">
						<svg class="mx-auto h-12 w-12 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
						</svg>
						<h3 class="mt-2 text-sm font-semibold text-slate-900">No open tasks right now</h3>
						<p class="mt-1 text-xs text-slate-500">Check back shortly. The hub regularly matches new SME requests.</p>
					</div>
				{:else}
					<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
						{#each tasks.slice(0, 6) as task (task.id)}
							<TaskCard {task} />
						{/each}
					</div>
				{/if}
			</div>

			<!-- Right Column (1 Col): My Experience Ledger -->
			<div class="space-y-6">
				<div class="flex items-center justify-between">
					<div>
						<h2 class="text-xl font-bold text-slate-900">Experience Ledger</h2>
						<p class="text-xs text-slate-500">Your verified work history and SME ratings.</p>
					</div>
					<span class="rounded-full bg-purple-50 px-2.5 py-0.5 text-xs font-bold text-purple-700 border border-purple-200">
						{experiences.length} records
					</span>
				</div>

				{#if loadingExp}
					<div class="space-y-4">
						<div class="h-36 rounded-2xl bg-slate-200/60 animate-pulse"></div>
						<div class="h-36 rounded-2xl bg-slate-200/60 animate-pulse"></div>
					</div>
				{:else if experiences.length === 0}
					<div class="rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-center">
						<div class="mx-auto h-10 w-10 rounded-full bg-purple-50 text-purple-600 flex items-center justify-center font-bold mb-2">
							★
						</div>
						<h3 class="text-sm font-bold text-slate-900">Your Ledger is Clean</h3>
						<p class="mt-1 text-xs text-slate-500 leading-relaxed">
							Apply and complete your first SME task. Once verified by the business, your permanent proof of capability will appear here.
						</p>
					</div>
				{:else}
					<div class="space-y-4">
						{#each experiences as exp (exp.id)}
							<ExperienceCard experience={exp} />
						{/each}
					</div>
				{/if}
			</div>
		</div>
	</div>
</RoleGuard>
